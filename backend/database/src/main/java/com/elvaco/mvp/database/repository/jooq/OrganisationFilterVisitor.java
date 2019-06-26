package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.OrganisationParentFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.tables.Organisation.ORGANISATION;

class OrganisationFilterVisitor extends EmptyFilterVisitor {

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(ORGANISATION.ID.in(filter.values()));
  }

  @Override
  public void visit(OrganisationParentFilter filter) {
    addCondition(ORGANISATION.PARENT_ID.isNull());
  }

  @Override
  public void visit(WildcardFilter filter) {
    addCondition(ORGANISATION.NAME.lower().contains(filter.oneValue().toLowerCase()));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query;
  }
}
