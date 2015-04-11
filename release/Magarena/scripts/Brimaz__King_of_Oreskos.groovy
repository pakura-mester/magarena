[
    new MagicWhenSelfBlocksTrigger() {
        @Override
        public MagicEvent executeTrigger(final MagicGame game,final MagicPermanent permanent,final MagicPermanent blocker) {
            final MagicPermanent blocked = permanent.getBlockedCreature();
            return new MagicEvent(
                permanent,
                blocked,
                this,
                "PN puts a 1/1 white Cat Soldier creature token with vigilance onto the battlefield blocking RN."
            );
        }
        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            final rn = event.getRefPermanent();
            game.doAction(new MagicPlayTokenAction(
                event.getPlayer(),
                TokenCardDefinitions.get("1/1 white Cat Soldier creature token with vigilance"),
                {
                    final MagicPermanent perm ->
                    final MagicGame G = perm.getGame();
                    G.doAction(new MagicSetBlockerAction(rn.map(G), perm));
                }
            ));
        }
    }
]
