package com.shield.tenant.filter;

import com.shield.tenant.context.TenantContext;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TenantHibernateFilterAspect {

    private final EntityManager entityManager;

    @Around("@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object enableTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID tenantId = TenantContext.getTenantId().orElse(null);
        if (tenantId == null) {
            return joinPoint.proceed();
        }

        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", tenantId);

        try {
            return joinPoint.proceed();
        } finally {
            session.disableFilter("tenantFilter");
        }
    }
}
