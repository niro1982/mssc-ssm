package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;

import java.util.EnumSet;

@Slf4j
@EnableStateMachineFactory //tells spring to have component to generate state machine
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        //telling the StateMachineStateConfigurer about the state machine

        states.withStates()
                .initial(PaymentState.NEW) //initial state of the state machine
                .states(EnumSet.allOf(PaymentState.class)) //load to a set all the states from PaymentState class
                .end(PaymentState.AUTH) //the ending of the state machine in this case if we get to 1 of
                .end(PaymentState.PRE_AUTH_ERROR) //3 states (auth, auth_error,pre_auth_error) the state machine
                .end(PaymentState.AUTH_ERROR); //is done
    }
}
