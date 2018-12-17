update physical_meter set active_period = p.period
from (
  select
  logical_meter.id,
  physical_meter.id as physical_meter_id,
  case (m.cmin is null)
    when true then 'empty'::tstzrange
    else tstzrange(
      m.cmin,
      lead(m.cmin) over (partition by logical_meter.id order by m.cmin asc)
    )
  end as period
  from logical_meter
  join physical_meter
  on (
    logical_meter.organisation_id = physical_meter.organisation_id
    and logical_meter.id = physical_meter.logical_meter_id
  )
  join lateral (
    select min(created) as cmin
    from measurement
    where (physical_meter.id = measurement.physical_meter_id)
  ) m on true
) p
where p.physical_meter_id = physical_meter.id;


