package com.elvaco.mvp.database.bindings;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.database.util.PeriodRangeParser;

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

public class TimestampWithTimeZoneRangeBinding implements Binding<Object, PeriodRange> {
  private static final long serialVersionUID = -2595032464739348537L;

  @Override
  public Converter<Object, PeriodRange> converter() {
    return new Converter<>() {
      private static final long serialVersionUID = 692440657729988729L;

      @Nullable
      @Override
      public PeriodRange from(Object o) {
        return o == null ? null : PeriodRangeParser.parse((String) o);
      }

      @Nullable
      @Override
      public Object to(PeriodRange pr) {
        return pr == null ? null : PeriodRangeParser.format(pr);
      }

      @Override
      public Class<Object> fromType() {
        return Object.class;
      }

      @Override
      public Class<PeriodRange> toType() {
        return PeriodRange.class;
      }
    };
  }

  @Override
  public void sql(BindingSQLContext<PeriodRange> ctx) throws SQLException {
    if (ctx.render().paramType() == ParamType.INLINED) {
      ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::tstzrange");
    } else {
      ctx.render().sql("cast(? as tstzrange)");
    }
  }

  @Override
  public void register(BindingRegisterContext<PeriodRange> ctx) throws SQLException {
    ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
  }

  @Override
  public void set(BindingSetStatementContext<PeriodRange> ctx) throws SQLException {
    ctx.statement()
      .setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
  }

  @Override
  public void set(BindingSetSQLOutputContext<PeriodRange> ctx) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public void get(BindingGetResultSetContext<PeriodRange> ctx) throws SQLException {
    ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
  }

  @Override
  public void get(BindingGetStatementContext<PeriodRange> ctx) throws SQLException {
    ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
  }

  @Override
  public void get(BindingGetSQLInputContext<PeriodRange> ctx) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }
}
