delete
  from measurement m
 where dimension(m.value) not in (
                                  select dimension(q.unit::unit)
                                    from quantity q
                                   where q.id = m.quantity
                                  );
