package com.elvaco.mvp.database.repository.jooq;

import javax.annotation.Nullable;

import org.jooq.Condition;

interface ConditionAdding {

  void addCondition(@Nullable Condition condition);
}
