update physical_meter
set active_period = p.period
from (select logical_meter.id,
             physical_meter.id as physical_meter_id,
             m.cmin,
             m.cmax,
             case (m.cmin is null)
               when true then 'empty'::tstzrange
               else tstzrange(
                 greatest(lead(m.cmax) over w, m.cmin),
                 case lag(m.cmin) over w is null
                   when true
                     then null
                   else greatest(m.cmax, lag(m.cmin) over w)
                   end
                 )
               end as period
      from logical_meter
             join physical_meter
                  on (
                        logical_meter.organisation_id = physical_meter.organisation_id
                      and logical_meter.id = physical_meter.logical_meter_id
                    )
             join lateral (
        select max(created) as cmax,
               min(created) as cmin
        from measurement
        where (physical_meter.id = measurement.physical_meter_id)
        ) m on true window w as (partition by logical_meter.id order by m.cmax desc)
      order by logical_meter.id,period desc) as p
where p.physical_meter_id = physical_meter.id;

