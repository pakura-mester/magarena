package magic.model.trigger;

import magic.model.MagicCardDefinition;
import magic.model.MagicGame;
import magic.model.MagicPayedCost;
import magic.model.MagicPermanent;
import magic.model.MagicPermanentState;
import magic.model.action.SacrificeAction;
import magic.model.action.ChangeStateAction;
import magic.model.action.MagicPermanentAction;
import magic.model.event.MagicEventAction;
import magic.model.event.MagicEvent;
import magic.model.event.MagicSourceEvent;
import magic.model.event.MagicSacrificePermanentEvent;
import magic.model.choice.MagicMayChoice;
import magic.model.choice.MagicTargetChoice;

public abstract class EntersBattlefieldTrigger extends MagicTrigger<MagicPayedCost> {

    public static EntersBattlefieldTrigger create(final MagicSourceEvent sourceEvent) {
        return new EntersBattlefieldTrigger() {
            @Override
            public MagicEvent executeTrigger(final MagicGame game,final MagicPermanent permanent, final MagicPayedCost data) {
                return sourceEvent.getTriggerEvent(permanent);
            }
        };
    }

    public static final EntersBattlefieldTrigger createKicked(final MagicSourceEvent sourceEvent) {
        return new EntersBattlefieldTrigger() {
            @Override
            public MagicEvent executeTrigger(final MagicGame game,final MagicPermanent permanent, final MagicPayedCost data) {
                return sourceEvent.getTriggerEvent(permanent);
            }
            @Override
            public boolean accept(final MagicPermanent permanent, final MagicPayedCost payedCost) {
                return payedCost.isKicked();
            }
        };
    }

    public EntersBattlefieldTrigger(final int priority) {
        super(priority);
    }

    public EntersBattlefieldTrigger() {}

    public MagicTriggerType getType() {
        return MagicTriggerType.WhenComesIntoPlay;
    }

    @Override
    public void change(final MagicCardDefinition cdef) {
        cdef.addTrigger(this);
    }

    public static final EntersBattlefieldTrigger ChooseOpponent = new EntersBattlefieldTrigger(MagicTrigger.REPLACEMENT) {
        @Override
        public MagicEvent executeTrigger(final MagicGame game, final MagicPermanent permanent, final MagicPayedCost payedCost) {
            permanent.setChosenPlayer(permanent.getOpponent());
            return MagicEvent.NONE;
        }
    };

    public static final EntersBattlefieldTrigger Evoke = new EntersBattlefieldTrigger() {
        @Override
        public MagicEvent executeTrigger(final MagicGame game,final MagicPermanent permanent,final MagicPayedCost payedCost) {
            return payedCost.isKicked() ?
                new MagicEvent(
                    permanent,
                    this,
                    "Sacrifice SN."
                ):
                MagicEvent.NONE;
        }

        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            game.doAction(new SacrificeAction(event.getPermanent()));
        }
    };

    public static final EntersBattlefieldTrigger Exploit = new EntersBattlefieldTrigger() {
        private final MagicEventAction EVENT_ACTION=new MagicEventAction() {
            @Override
            public void executeEvent(final MagicGame game, final MagicEvent event) {
                event.processTargetPermanent(game,new MagicPermanentAction() {
                    public void doAction(final MagicPermanent permanent) {
                        game.doAction(ChangeStateAction.Set(event.getPermanent(), MagicPermanentState.Exploit));
                        game.doAction(new SacrificeAction(permanent));
                    }
                });
            }
        };

        @Override
        public MagicEvent executeTrigger(final MagicGame game,final MagicPermanent permanent,final MagicPayedCost payedCost) {
            return new MagicEvent(
                permanent,
                new MagicMayChoice("Sacrifice a creature?"),
                this,
                "PN may$ sacrifice a creature."
            );
        }

        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            final MagicEvent sac = new MagicSacrificePermanentEvent(
                event.getSource(),
                event.getPlayer(),
                MagicTargetChoice.A_CREATURE_YOU_CONTROL,
                EVENT_ACTION
            );
            if (event.isYes() && sac.isSatisfied()) {
                game.addEvent(sac);
            }
        }
    };
}