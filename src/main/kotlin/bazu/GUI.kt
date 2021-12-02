package bazu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material;
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable

var playerHeads: ArrayList<ItemStack> = arrayListOf()

fun setplayerHeads(){
    playerHeads.clear()

    for (i in Bukkit.getOnlinePlayers()) {
        val playerHead = ItemStack(Material.PLAYER_HEAD)
        val meta = playerHead.itemMeta
        if (meta is SkullMeta) {
            meta.owningPlayer = i
            playerHead.itemMeta = meta
        }
        playerHeads.add(playerHead)
    }
}

class GUI: Listener {

    var choosedPlayer: Player? = null
    var choosedNumber = 32

    val up = genMeta(ItemStack(Material.GREEN_WOOL),Component.text("숫자를 올립니다."))
    val down = genMeta(ItemStack(Material.RED_WOOL),Component.text("숫자를 내립니다."))
    val num = genMeta(ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE),Component.text("숫자입니다."))
    val accept = genMeta(ItemStack(Material.GREEN_STAINED_GLASS_PANE), Component.text("확인 버튼입니다."))
    var finished: Boolean = false
    var decisionSucceed: SucceedProperty = SucceedProperty.NOT_YET
    val yesNoBool = hashMapOf<ItemStack, Boolean>(genMeta(ItemStack(Material.GREEN_STAINED_GLASS_PANE), Component.text("예")) to true
        , genMeta(ItemStack(Material.RED_STAINED_GLASS_PANE), Component.text("아니오")) to false)

    var unacceptedPolicy: Right? = null

    var no_close = true

    var subject: Component? = null


    fun leaderVoteClick(i: ItemStack): Boolean{
        if (i.itemMeta is SkullMeta) {
            val a = voteNum[(i.itemMeta as SkullMeta).owningPlayer as Player]!!
            voteNum[(i.itemMeta as SkullMeta).owningPlayer as Player] = a+1

            if (voted == Bukkit.getOnlinePlayers().size-1){
                val max: MutableMap<Player, Int> = mutableMapOf(voteNum.entries.first().key to voteNum.entries.first().value)

                for (j in voteNum){
                    for (k in max){
                        if (j.value > k.value){
                            max.clear()
                            max.put(k.key, k.value)
                        }else if (j.value == k.value){
                            max.put(k.key, k.value)
                        }
                    }
                }
                setLeaderWithMessage(max.entries.random().key)
                resetVoteNum()
            }else if (voted < Bukkit.getOnlinePlayers().size-1){
                voted++
                Bukkit.broadcast(Component.text("${voted}명 투표함!"))
            }

            return true
        }
        return false
    }

    fun yesOrNoVoteClick(item: ItemStack): Boolean{
        val a = YesNoVote[gui.yesNoBool[item]!!]!!

        YesNoVote[gui.yesNoBool[item]!!] = a+1

        if (voted == Bukkit.getOnlinePlayers().filter { it.scoreboardTags.contains("conference") }.size-1) {
            var max: MutableMap.MutableEntry<Boolean, Int> = YesNoVote.entries.first()
            for (i in YesNoVote){
                if (i.value > max.value){
                    max = i
                }
            }

            if (max.key) {
                Bukkit.broadcast(Component.text("가결되었습니다!"))
                gui.unacceptedPolicy!!.run()
            }else{
                Bukkit.broadcast(Component.text("부결되었습니다!"))
            }
            resetVoteNum()

            for (i in Bukkit.getOnlinePlayers()) i.scoreboardTags.remove("conference")

        }
        else{
            voted++
            Bukkit.broadcast(Component.text("${voted}명 투표함!"))
        }

        return true

    }

    fun removePoint(player: Player, cnt: Int): Boolean{

        val failedItem = player.inventory.removeItemAnySlot(ItemStack(Material.EMERALD, cnt))
        if (failedItem[0] != null){
            treasury.value!!.addItem(ItemStack(Material.EMERALD, cnt-failedItem[0]!!.amount))
            if (cnt-failedItem[0]!!.amount > 0) {
                leader!!.sendMessage(Component.text("${player.name}에게 ${cnt}만큼의 에메랄드를 뺏었습니다."))
                player.sendMessage(Component.text("무언가 없어진 듯한 기분이 듭니다."))
            }else{
                leader!!.sendMessage(Component.text("돈이 없었군요.."))
                return false
            }
        }else{
            treasury.value!!.addItem(ItemStack(Material.EMERALD, cnt))
            leader!!.sendMessage(Component.text("${player.name}에게 ${cnt}만큼의 에메랄드를 뺏었습니다."))
            player.sendMessage(Component.text("무언가 없어진 듯한 기분이 듭니다."))
        }
        return true
    }

    fun addPoint(player: Player, cnt: Int): Boolean{
        val failedItem = treasury.value!!.removeItemAnySlot(ItemStack(Material.EMERALD, cnt))
        if (failedItem[0] != null){
            player.inventory.addItem(ItemStack(Material.EMERALD, cnt-failedItem[0]!!.amount))
            if (cnt > failedItem[0]!!.amount) {
                leader!!.sendMessage(Component.text("${player.name}에게 ${cnt - failedItem[0]!!.amount}만큼의 에메랄드를 주었습니다."))
                player.sendMessage(Component.text("무언가 생긴 듯한 기분이 듭니다."))
            }else{
                leader!!.sendMessage(Component.text("국고에 돈이 없었군요.."))
                return false
            }
        }else{
            player.inventory.addItem(ItemStack(Material.EMERALD, cnt))
            leader!!.sendMessage(Component.text("${player.name}에게 ${cnt}만큼의 에메랄드를 주었습니다."))
            player.sendMessage(Component.text("무언가 생긴 듯한 기분이 듭니다."))
        }
        return true
    }

    fun sendPrision(player: Player, term: Int){
        prisonTerm.value!!.getScore(player.name).score = term*20*60
        player.addScoreboardTag("prison")
    }

    fun changeTax(newTax: Int){
        Bukkit.broadcast(Component.text("세금이${tax}에서 ${newTax}로 변경되었습니다!"))
        tax = newTax

        taxRunnable.cancel()

        taxRunnable = TaxRunnable()
        taxTerm = (newTax*Bukkit.getOnlinePlayers().size/13+1).toDouble()
        tax = newTax

        Bukkit.broadcast(Component.text("세금은 이제부터 ${taxTerm}분마다 걷어집니다!"))
        taxRunnable.runTaskTimer(inst.value, (taxTerm*20*60).toLong(), (taxTerm*20*60).toLong())


        //3 : 10 == taxTerm : newTax
        //taxTerm = newTax*10/3
    }

    fun choicePerson(player: Player){
        val inv = Bukkit.createInventory(leader, 9 * (playerHeads.size / 5 + 2), Component.text("사람 선택"))
        var idx = 0
        for (i in playerHeads) {
            inv.setItem(idx, i)
            idx += 2
        }

        player.openInventory(inv)
    }

    fun choiceNumber(player: Player){
        val inv = Bukkit.createInventory(leader, 9 * 2, Component.text("숫자 선택"))

        gui.choosedNumber = 32

        num.amount = gui.choosedNumber

        inv.setItem(2, up)
        inv.setItem(4, num)
        inv.setItem(6, down)
        inv.setItem(9+4, accept)

        player.openInventory(inv)
    }

    fun choiceNumberClick(event: InventoryClickEvent){
        when(event.currentItem){
            up -> {
                if (gui.choosedNumber < 64) {
                    gui.choosedNumber++
                    num.amount = gui.choosedNumber
                }
            }
            down -> {
                if (gui.choosedNumber > 0) {
                    gui.choosedNumber--
                    num.amount = gui.choosedNumber
                }
            }
            accept -> {
                no_close = false

                event.inventory.close()
                gui.finished = true
            }
        }
    }

    @EventHandler
    fun onClickItem(event: InventoryClickEvent){

        for (i in Windows.values()){
            if (event.view.title() == i.label){
                event.isCancelled = true

                if (i.items.contains(event.currentItem) && event.currentItem != null && event.whoClicked is Player)
                    if (i.click(event.currentItem!!, event.whoClicked as Player)) event.inventory.close()
            }
        }

        if (event.view.title() == Component.text("사람 선택")){
            event.isCancelled = true
            for (i in playerHeads) {
                if (event.currentItem == i){
                    gui.choosedPlayer = ((i.itemMeta as SkullMeta).owningPlayer as Player)
                    gui.finished = true
                }
            }

            no_close = false
            event.inventory.close()
        }

        else if (event.view.title() == Component.text("숫자 선택")) {
            event.isCancelled = true

            choiceNumberClick(event)

            event.inventory.setItem(2, up)
            event.inventory.setItem(4, num)
            event.inventory.setItem(6, down)
            event.inventory.setItem(9 + 4, accept)
        }
        else if (event.view.title() == Component.text("국고")) event.isCancelled = true
    }
}



enum class Right(var item:ItemStack, val needVote: Boolean): RightFunction{
    CHANGE_LEADER(ItemStack(Material.PLAYER_HEAD), false){
        init {
            item = genMeta(item, Component.text("리더 바꾸기", TextColor.color(0xff, 0xff, 0x0))
                    , Component.text("현재 리더가 마음에 안드시나요?"), Component.text("리더를 바꿀 수 있습니다!"))
        }

        override fun run() {
            var bool = true
            gui.choicePerson(leader!!)
            class isPlayerChoiced: BukkitRunnable() {
                override fun run() {
                    if (gui.finished) {
                        this.cancel()
                        gui.finished = false

                        leader!!.removeScoreboardTag("leader")
                        setLeaderWithMessage(gui.choosedPlayer!!)

                        gui.decisionSucceed = SucceedProperty.SUCCEED

                    }else if (leader!!.openInventory.title() != Component.text("사람 선택")){
                        this.cancel()
                        gui.decisionSucceed = SucceedProperty.FAILED
                    }
                }
            }

            isPlayerChoiced().runTaskTimer(inst.value, 0, 1)
        }
    },
    ADD_POINT(ItemStack(Material.EMERALD), false){
        init {
            item = genMeta(item, Component.text("에메랄드 주기", TextColor.color(0xff, 0xff, 0x0))
                    , Component.text("에메랄드를 줘야 할 사람이 있다고요?")
                    , Component.text("어떤 목적이든 이걸 선택하시면 국고에서 에메랄드를 줄 수 있습니다."))
        }

        override fun run() {
            var bool = true

            gui.choicePerson(leader!!)
            class isPlayerChoiced: BukkitRunnable(){
                override fun run() {
                    if (gui.finished){
                        this.cancel()
                        gui.finished = false

                        gui.choiceNumber(leader!!)
                        class isNumChoiced: BukkitRunnable(){
                            override fun run() {
                                if (gui.finished){
                                    bool = gui.addPoint(gui.choosedPlayer!!, gui.choosedNumber)
                                    this.cancel()
                                    gui.finished = false
                                    gui.decisionSucceed = SucceedProperty.SUCCEED
                                }else if (leader!!.openInventory.title() != Component.text("숫자 선택")){
                                    this.cancel()
                                    gui.decisionSucceed = SucceedProperty.FAILED
                                }
                            }
                        }

                        isNumChoiced().runTaskTimer(inst.value, 0, 1)
                    }else if (leader!!.openInventory.title() != Component.text("사람 선택")){
                        this.cancel()
                        gui.decisionSucceed = SucceedProperty.FAILED
                    }
                }
            }
            isPlayerChoiced().runTaskTimer(inst.value, 0, 1)
        }
    },
    REMOVE_POINT(ItemStack(Material.EMERALD), true){
        init {
            item = genMeta(item, Component.text("에메랄드 뻈기", TextColor.color(0xff, 0xff, 0x0))
                , Component.text("에메랄드를 뺏어야 할 사람이 있다고요?")
                , Component.text("어떤 목적이든 이걸 선택하시면 국고로 에메랄드를 뺏을 수 있습니다.")
                , Component.text("체제가 민주주의일 경우 민중의 과반수의 동의가 필요합니다."))
        }

        override fun run() {
            var bool = true

            gui.choicePerson(leader!!)
            class isPlayerChoiced: BukkitRunnable(){
                override fun run() {
                    if (gui.finished){
                        this.cancel()
                        gui.finished = false

                        gui.choiceNumber(leader!!)
                        class isNumChoiced: BukkitRunnable(){
                            override fun run() {
                                if (gui.finished){
                                    bool = gui.removePoint(gui.choosedPlayer!!, gui.choosedNumber)
                                    this.cancel()
                                    gui.finished = false
                                    gui.decisionSucceed = SucceedProperty.SUCCEED
                                }else if (leader!!.openInventory.title() != Component.text("숫자 선택")){
                                    this.cancel()
                                    gui.decisionSucceed = SucceedProperty.FAILED
                                }
                            }
                        }

                        isNumChoiced().runTaskTimer(inst.value, 0, 1)
                    }else if (leader!!.openInventory.title() != Component.text("사람 선택")){
                        this.cancel()
                        gui.decisionSucceed = SucceedProperty.FAILED
                    }
                }
            }
            isPlayerChoiced().runTaskTimer(inst.value, 0, 1)
        }
    },
    PRISON(ItemStack(Material.IRON_BARS), true){
        init {
            item = genMeta(item, Component.text("감옥 보내기", TextColor.color(0xff, 0xff, 0x0))
                    , Component.text("사람을 감옥에 보낼 수 있습니다!")
                    , Component.text("주의사항: 감옥에 보내는 기간은 분 단위입니다.")
                    , Component.text("체제가 민주주의일 경우 민중의 과반수의 동의가 필요합니다."))
        }

        override fun run() {
            gui.choicePerson(leader!!)
            var bool = true
            class isPlayerChoiced: BukkitRunnable(){
                override fun run() {
                    if (gui.finished){
                        this.cancel()
                        gui.finished = false

                        gui.choiceNumber(leader!!)
                        class isNumChoiced: BukkitRunnable(){
                            override fun run() {
                                if (gui.finished){
                                    gui.sendPrision(gui.choosedPlayer!!, gui.choosedNumber)
                                    this.cancel()
                                    gui.finished = false
                                    gui.decisionSucceed = SucceedProperty.SUCCEED
                                }else if (leader!!.openInventory.title() != Component.text("숫자 선택")){
                                    this.cancel()
                                    gui.decisionSucceed = SucceedProperty.FAILED
                                }
                            }
                        }

                        isNumChoiced().runTaskTimer(inst.value, 0, 1)
                    }else if (leader!!.openInventory.title() != Component.text("사람 선택")){
                        this.cancel()
                        gui.decisionSucceed = SucceedProperty.FAILED
                    }
                }
            }

            isPlayerChoiced().runTaskTimer(inst.value, 0, 1)
        }
    },
    SET_TAX(ItemStack(Material.EMERALD), true) {
        init {
            item = genMeta(item, Component.text("세금 설정", TextColor.color(0xff, 0xff, 0x0))
                    , Component.text("세금을 더 많이 걷고 싶으신가요?")
                    , Component.text("아니면 세금이 너무 과다한가요?")
                    , Component.text("여기서 세금을 변경하세요!")
                    , Component.text("체제가 민주주의일 경우 민중의 과반수의 동의가 필요합니다."))
        }

        override fun run() {
            gui.choiceNumber(leader!!)
            var bool = true
            class isNumberChoiced: BukkitRunnable(){
                override fun run() {
                    if (gui.finished){
                        this.cancel()
                        gui.finished = false

                        gui.changeTax(gui.choosedNumber)
                        gui.decisionSucceed = SucceedProperty.SUCCEED
                    }else if (leader!!.openInventory.title() != Component.text("숫자 선택")){
                        this.cancel()
                        gui.decisionSucceed = SucceedProperty.FAILED
                    }
                }
            }

            isNumberChoiced().runTaskTimer(inst.value, 0, 1)
        }
    },
    CHANGE_SYSTEM(ItemStack(Material.RED_BANNER), true){
        init {
            item = genMeta(item, Component.text("체제 변경", TextColor.color(0xff, 0xff, 0x0))
                    , Component.text("민주주의나 독재로 체제를 바꾸세요!")
                    , Component.text("조심하세요. 담신의 시대가 끝날 수 있습니다!")
                    , Component.text("체제가 민주주의일 경우 민중의 과반수의 동의가 필요합니다."))
        }

        override fun run() {
            if (system == System.DICTORSHIP){
                system = System.DEMOCRACY
                Bukkit.broadcast(Component.text("체제가 민주주의로 바뀌었습니다."))
                voteRunnable.runTaskTimer(inst.value, 0, (9*20*60).toLong())
            }
            else if (system == System.DEMOCRACY){
                system = System.DICTORSHIP
                Bukkit.broadcast(Component.text("체제가 독재로 바뀌었습니다."))
                voteRunnable.cancel()
            }
            gui.decisionSucceed = SucceedProperty.SUCCEED
        }
    },
    OPEN_TREASURY(ItemStack(Material.CHEST), false){
        init{
            item = genMeta(item, Component.text("국고 열기", TextColor.color(0xff, 0xff, 0x0))
                , Component.text("현재 국가의 재정 상태는 어떤지 봅니다."))
        }

        override fun run() {
            leader!!.openInventory(treasury.value!!)
            gui.decisionSucceed = SucceedProperty.SUCCEED
        }
    },
    COUNTER_ATTACK(ItemStack(Material.IRON_SWORD), false){
        init{
            item = genMeta(item, Component.text("우민마을 공격하기", TextColor.color(0xff, 0xff, 0x0))
                , Component.text("우민마을을 공격해 에메랄드를 약탈해 올 수 있습니다.")
                , Component.text("하지만 조심하세요.. 누구나 자신의 것을 뺏기면 화나는 법이니까요.."))
        }

        override fun run() {
            attack.counterAttack()
            gui.decisionSucceed = SucceedProperty.SUCCEED
        }
    }
}

interface RightFunction{
    fun run()
}
















enum class Windows(val label: Component, var items: ArrayList<ItemStack>, val no_close: Boolean, val size: Int): WindowsFunction{
    LEADER_VOTE(Component.text("지도자 투표")
        , arrayListOf<ItemStack>(), true, 9 + (playerHeads.size/9).toInt()){
        init {
          for (i in playerHeads) items.add(i)
        }

        override fun run(player: ArrayList<Player>) {
            for (i in player) {
                val voteInv = Bukkit.createInventory(i, size, this.label)
                var index = 0
                for (j in playerHeads) {
                    voteInv.setItem(index, j)
                    index += 2
                }
                i.openInventory(voteInv)
            }
        }

        override fun click(item: ItemStack, player: Player): Boolean {
            return gui.leaderVoteClick(item)
        }
    },
    YES_OR_NO_VOTE(Component.text("찬반 투표")
        , arrayListOf(genMeta(ItemStack(Material.GREEN_STAINED_GLASS_PANE), Component.text("예")),
            genMeta(ItemStack(Material.RED_STAINED_GLASS_PANE), Component.text("아니오"))), true, 9) {
        override fun run(player: ArrayList<Player>) {
            for (i in Bukkit.getOnlinePlayers()) {
                if (i.scoreboardTags.contains("conference")) {
                    val voteInv = Bukkit.createInventory(i, 9, this.label)

                    val item = genMeta(ItemStack(Material.LIGHT_GRAY_DYE), Component.text("주제"), gui.subject!!)

                    voteInv.setItem(2, this.items[0])
                    voteInv.setItem(4, item)
                    voteInv.setItem(6, this.items[1])

                    i.openInventory(voteInv)
                }
            }
        }

        override fun click(item: ItemStack, player: Player): Boolean {
            return gui.yesOrNoVoteClick(item)
        }
    },
    LEADER_DECISION(Component.text("지도자의 결정"), arrayListOf<ItemStack>(), false, 9) {
        init{
            for (i in Right.values()){
                this.items.add(i.item)
            }
        }

        override fun run(player: ArrayList<Player>) {
            gui.decisionSucceed = SucceedProperty.NOT_YET

            val inv = Bukkit.createInventory(leader, 9, this.label)

            for (i in 0 until Right.values().size) inv.setItem(i, Right.values()[i].item)

            leader!!.openInventory(inv)
        }

        override fun click(item: ItemStack, player: Player): Boolean {
            for (i in Right.values()){
                if (item == i.item){
                    if (system == System.DICTORSHIP) {
                        i.run()

                        class isSucceed: BukkitRunnable(){
                            override fun run() {
                                if (gui.decisionSucceed != SucceedProperty.NOT_YET) this.cancel()
                                if (gui.decisionSucceed == SucceedProperty.FAILED)
                                    Windows.LEADER_DECISION.run(arrayListOf(leader!!)) }
                        }

                        isSucceed().runTaskTimer(inst.value, 0, 1)
                    }else{
                        if (i.needVote){
                            gui.subject = i.item.displayName()
                            gui.unacceptedPolicy = i

                            val array = arrayListOf<Player>()
                            array.addAll(Bukkit.getOnlinePlayers().filter { !it.scoreboardTags.contains("prison") })

                            Windows.JOIN_CONFERENCE.run(array)
                        }else{
                            i.run()
                        }
                    }
                }
            }

            return true
        }
    },
    JOIN_CONFERENCE(Component.text("회의 참가"), arrayListOf(genMeta(ItemStack(Material.GREEN_STAINED_GLASS_PANE), Component.text("회의에 참가하시겠습니까?")
        , Component.text("거부하려면 인벤토리를 닫으세요."))), false, 9) {
        override fun run(player: ArrayList<Player>) {
            for (i in player) {
                val inv = Bukkit.createInventory(i, 9, this.label)

                inv.setItem(4, this.items[0])

                inv.setItem(8, genMeta(ItemStack(Material.LIGHT_GRAY_DYE), Component.text("주제"), gui.subject!!))

                i.openInventory(inv)
            }
        }

        override fun click(item: ItemStack, player: Player): Boolean {
            if (item == this.items[0]) {
                player.teleport(
                    org.bukkit.Location(org.bukkit.Bukkit.getWorld("world"), -47.5, 67.0, 15.5)
                )
                player.addScoreboardTag("conference")

                return true
            }
            return false
        }
    },
    REVOLUTION(Component.text("혁명 진영 선택"), arrayListOf(
        genMeta(ItemStack(Material.BLUE_BANNER), Component.text("혁명군진영 참여")),
        genMeta(ItemStack(Material.RED_BANNER), Component.text("정부군진영 참여")),
        genMeta(ItemStack(Material.GRAY_BANNER), Component.text("중립진영 참여"))
    ), true, 9) {
        override fun run(player: ArrayList<Player>) {
            val revPlayer = Bukkit.getOnlinePlayers().find { it.scoreboardTags.contains("revolutionFirst") }

            for (i in player) {
                val inv = Bukkit.createInventory(i, 9, this.label)

                inv.setItem(2, genMeta(ItemStack(Material.BLUE_BANNER), Component.text("혁명군진영 참여")))
                inv.setItem(4, genMeta(ItemStack(Material.GRAY_BANNER), Component.text("중립진영 참여")))
                inv.setItem(6, genMeta(ItemStack(Material.RED_BANNER), Component.text("정부군진영 참여")))
                inv.setItem(8, genMeta(ItemStack(Material.LIGHT_GRAY_DYE), Component.text("혁명이 일어났습니다!")
                    , Component.text(revPlayer!!.name+"님이 일으킨 혁명에 참가하시겠습니까?")
                    , Component.text("아니면 끝까지 리더 "+leader!!.name+"님을 따르시겠습니까?")
                    , Component.text("아니면 둘 다 선택하지 않을 수도 있죠")))

                i.openInventory(inv)
            }
        }

        override fun after() {
            class revolutionStart: BukkitRunnable() {
                override fun run() {
                    if (Bukkit.getOnlinePlayers().filter
                    {
                        !revolutionTeam.value!!.entries.contains(it.name)
                                && !leaderTeam.value!!.entries.contains(it.name)
                                && !midTeam.value!!.entries.contains(it.name)
                    }.isEmpty())
                    {
                        for (i in revolutionTeam.value!!.entries)
                            Bukkit.broadcast(Component.text("${i}님은 혁명 진영입니다!", TextColor.color(0x00, 0x00, 0xff)))
                        for (i in leaderTeam.value!!.entries)
                            Bukkit.broadcast(Component.text("${i}님은 국가 진영입니다!", TextColor.color(0xff, 0x00, 0x00)))
                        for (i in midTeam.value!!.entries)
                            Bukkit.broadcast(Component.text("${i}님은 중도입니다!"))
                        revolutioning = true

                        this.cancel()

                        class isFinishedRevolutioning : BukkitRunnable() {
                            override fun run() {
                                if (revolutionTeam.value!!.entries.isEmpty()) {
                                    revolutioning = false

                                    for (i in Bukkit.getOnlinePlayers()) {
                                        i.gameMode = GameMode.ADVENTURE
                                    }
                                    for (i in revolutionTeam.value!!.entries) revolutionTeam.value!!.removeEntry(i)
                                    for (i in leaderTeam.value!!.entries) leaderTeam.value!!.removeEntry(i)
                                    Bukkit.broadcast(Component.text("국가 진영이 승리했습니다!", TextColor.color(0xff, 0x00, 0x00)))
                                    setLeaderWithMessage(leader!!)
                                    this.cancel()
                                } else if (leaderTeam.value!!.entries.isEmpty()) {
                                    revolutioning = false
                                    setLeaderWithMessage(Bukkit.getOnlinePlayers().filter {
                                        it.scoreboardTags.contains("revolutionFirst")
                                    }[0])
                                    leader!!.scoreboardTags.remove("revolutionFirst")

                                    for (i in Bukkit.getOnlinePlayers()) {
                                        i.gameMode = GameMode.ADVENTURE
                                    }
                                    for (i in revolutionTeam.value!!.entries) revolutionTeam.value!!.removeEntry(i)
                                    for (i in leaderTeam.value!!.entries) leaderTeam.value!!.removeEntry(i)
                                    for (i in midTeam.value!!.entries) leaderTeam.value!!.removeEntry(i)
                                    Bukkit.broadcast(Component.text("혁명 진영이 승리했습니다!", TextColor.color(0x00, 0x00, 0xff)))
                                    this.cancel()
                                }
                            }
                        }

                        isFinishedRevolutioning().runTaskTimer(inst.value, 0, 1)
                    }
                }
            }

            revolutionStart().runTaskTimer(inst.value, 0, 1)
        }

        override fun click(item: ItemStack, player: Player): Boolean {

            when(item) {
                this.items[0] ->{
                    bazu.revolutionTeam.value!!.addEntry(player.name)
                }

                this.items[1] -> {
                    bazu.leaderTeam.value!!.addEntry(player.name)
                }

                this.items[2] -> {
                    bazu.midTeam.value!!.addEntry(player.name)
                }
            }

            return this.items.contains(item)
        }
    }
}

interface WindowsFunction{
    fun run(player: ArrayList<Player>)

    fun after(){

    }

    fun click(item: ItemStack, player: Player): Boolean
}