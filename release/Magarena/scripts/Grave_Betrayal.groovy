def DelayedTrigger = {
    final MagicPermanent source, final MagicPlayer player, final MagicCard card ->
    return new MagicAtEndOfTurnTrigger() {
        @Override
        public MagicEvent executeTrigger(final MagicGame game, final MagicPermanent permanent, final MagicPlayer eotPlayer) {
            game.addDelayedAction(new MagicRemoveTriggerAction(this));
            
            final MagicCard mappedCard = card.getOwner().map(game).getGraveyard().getCard(card.getId());
            
            return mappedCard.isInGraveyard() ?
                new MagicEvent(
                    game.createDelayedSource(source, player)
                    mappedCard,
                    this,
                    "Return RN to the battlefield under PN's control with an additional +1/+1 counter on it. " +
                    "That creature is a black Zombie in addition to its other colors and types."
                ):
                MagicEvent.NONE;
        }
        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            game.doAction(new MagicReanimateAction(
                event.getRefCard(),
                event.getPlayer(),
                [MagicPlayMod.UNDYING, MagicPlayMod.BLACK, MagicPlayMod.ZOMBIE]
            ));
        }
    };
}

[
    new MagicWhenOtherDiesTrigger() {
        @Override
        public MagicEvent executeTrigger(final MagicGame game,final MagicPermanent permanent,final MagicPermanent otherPermanent) {
            return (otherPermanent.isCreature() && permanent.isEnemy(otherPermanent)) ?
                new MagicEvent(
                    permanent,
                    otherPermanent.getCard(),
                    this,
                    "Return RN to the battlefield under PN's control with an additional +1/+1 counter on it at the beginning of the next end step. " + 
                    "That creature is a black Zombie in addition to its other colors and types."
                ) :
                MagicEvent.NONE;
        }
        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            game.doAction(new MagicAddTriggerAction(DelayedTrigger(
                event.getPermanent(),
                event.getPlayer(),
                event.getRefCard()
            )));
        }
    }
]
