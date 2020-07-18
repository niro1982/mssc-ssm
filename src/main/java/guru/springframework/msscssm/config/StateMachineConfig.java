package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

/**
 * Created by jt on 2019-07-23.
 */
@Slf4j
@RequiredArgsConstructor
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    //doing Autowire by name and not by type cause all of my instances are Actions.
    //spring will try to wire by type first and when it cant it will look for spring beans (components) with the same name here with lower case.
    //if i give the instances a name that is different from the class name, i will need to initialize it in the constructor and to use @qualifier
    //to let spring know exactly which class to autowire since they are all instances of Action class.
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
    private final Action<PaymentState, PaymentEvent> authApprovedAction;
    private final Action<PaymentState, PaymentEvent> authDeclinedAction;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE)
                    .action(preAuthAction).guard(paymentIdGuard)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED).action(preAuthApprovedAction)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED).action(preAuthDeclinedAction)
                //preauth to auth
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE)
                    .action(authAction)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED).action(authApprovedAction)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED).action(authDeclinedAction);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged(from: %s, to: %s)", from, to));
            }
        };

        config.withConfiguration()
                .listener(adapter);
    }

/*    public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
        return context -> {
            return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
        };
    }*/

/*    public Action<PaymentState, PaymentEvent> preAuthAction(){
        return context -> {
            System.out.println("PreAuth was called!!!");

            if (new Random().nextInt(10) < 8) {
                System.out.println("Pre Auth Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                    .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                    .build());

            } else {
                System.out.println("Per Auth Declined! No Credit!!!!!!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }*/

    /*public Action<PaymentState, PaymentEvent> authAction(){
        return context -> {
            System.out.println("Auth was called!!!");

            if (new Random().nextInt(10) < 8) {
                System.out.println("Auth Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());

            } else {
                System.out.println("Auth Declined! No Credit!!!!!!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }*/
}
