package com.udacity.jdnd.course3.critter.schedule;

import com.udacity.jdnd.course3.critter.pet.Pet;
import com.udacity.jdnd.course3.critter.user.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public class CustomScheduleRepositoryImpl implements CustomScheduleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Schedule> customScheduleSearch(ScheduleQuery scheduleQuery) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Schedule> criteria = builder.createQuery(Schedule.class);
        Root<Schedule> schedule = criteria.from(Schedule.class);
        List<Predicate> predicates = new ArrayList<>();
        List<Order> orderList = new ArrayList();

        if (scheduleQuery.getDate() != null) {
            predicates.add(builder.equal(schedule.get("date"), scheduleQuery.getDate()));
        }
        if (scheduleQuery.getStatus() != null) {
            predicates.add(builder.equal(schedule.get("status"), scheduleQuery.getStatus()));
        }
        if (scheduleQuery.getEmployee() != null) {
            Join<Schedule,Employee> join = schedule.join( "employees");
            criteria.select(schedule);
            predicates.add(builder.equal(join.get("id"), scheduleQuery.getEmployee().getId()));
        }
        if (scheduleQuery.getPet() != null) {
            Join<Schedule,Pet> join = schedule.join( "pets");
            criteria.select(schedule);
            predicates.add(builder.equal(join.get("id"), scheduleQuery.getPet().getId()));
        }
        if (scheduleQuery.getCustomer() != null) {
            Join<Schedule,Pet> join = schedule.join( "pets");
            criteria.select(schedule);
            predicates.add(builder.equal(join.get("customer"), scheduleQuery.getCustomer()));
        }
        if (scheduleQuery.getDateTimeOrder() != null) {
            if (scheduleQuery.getDateTimeOrder().equals("DESC")) {
                orderList.add(builder.desc(schedule.get("date")));
                orderList.add(builder.desc(schedule.get("startTime")));
            } else {
                orderList.add(builder.asc(schedule.get("date")));
                orderList.add(builder.asc(schedule.get("startTime")));
            }
            criteria.orderBy(orderList);
        }

        criteria.where(predicates.toArray(new Predicate[0]));
        TypedQuery<Schedule> query = entityManager.createQuery(criteria)
                .setMaxResults(scheduleQuery.getLimit())
                .setFirstResult(scheduleQuery.getOffset());
        return query.getResultList();
    }
}