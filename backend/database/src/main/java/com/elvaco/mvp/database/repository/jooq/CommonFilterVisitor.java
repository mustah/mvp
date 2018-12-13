package com.elvaco.mvp.database.repository.jooq;

abstract class CommonFilterVisitor
  extends JooqFilterVisitor
  implements CommonLocationFilterVisitor,
             CommonGatewayFilterVisitor,
             PhysicalMeterFilterVisitor,
             CommonLogicalMeterFilterVisitor {
}
