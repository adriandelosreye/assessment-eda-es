package ec.com.sofka.aggregate;

import ec.com.sofka.account.Account;
import ec.com.sofka.aggregate.events.AccountCreated;
import ec.com.sofka.aggregate.events.AccountRetrieved;
import ec.com.sofka.aggregate.events.UserCreated;
import ec.com.sofka.aggregate.events.UserRetrieved;
import ec.com.sofka.aggregate.values.CustomerId;
import ec.com.sofka.generics.domain.DomainEvent;
import ec.com.sofka.generics.utils.AggregateRoot;
import ec.com.sofka.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

//2. Creation of the aggregate class - The communication between the entities and the external world.
public class Customer extends AggregateRoot<CustomerId> {
    //5. Add the Account to the aggregate: Can't be final bc the aggregate is mutable by EventDomains
    private Account account;
    private User user;

    //To create the Aggregate the first time, ofc have to set the id as well.
    public Customer() {
        super(new CustomerId());
        //Add the handler to the aggregate
        setSubscription(new CustomerHandler(this));
    }

    //To rebuild the aggregate
    private Customer(final String id) {
        super(CustomerId.of(id));
        //Add the handler to the aggregate
        setSubscription(new CustomerHandler(this));
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    //Remember that User as Aggregate is the open door to interact with the entities
    public void createAccount(BigDecimal balance, String accountNumber, String userId) {
        //Add the event to the aggregate
        addEvent(new AccountCreated(accountNumber, balance, userId)).apply();
    }

    public void retrieveAccount(BigDecimal balance, String accountNumber, String userId) {
        addEvent(new AccountRetrieved(accountNumber, balance, userId)).apply();
    }

    public void createUser(String name, String documentId){
        addEvent(new UserCreated(name, documentId)).apply();
    }

    public void retrieveUser(String name, String documentId){
        addEvent(new UserRetrieved(name, documentId)).apply();
    }

    //To rebuild the aggregate
    public static Mono<Customer> from(final String id, Flux<DomainEvent> events) {
        Customer customer = new Customer(id);
        return events
                .flatMap(event -> Mono.fromRunnable(() -> customer.addEvent(event).apply()))
                .then(Mono.just(customer));
    }
}
