update physical_meter
set active_period = p.period
from (select logical_meter.id,
             physical_meter.id as physical_meter_id,
             m.cmin,
             m.cmax,
             case (m.cmin is null)
               when true then 'empty'::tstzrange
               else
                 tstzrange(
                   --Start period at the previously active meter's latest measurement, if it exists.
                   -- Otherwise, start at this meter's earliest measurement
                   coalesce(lead(m.cmax) over w, m.cmin),
                   case lag(m.cmin) over w is null
                     when true
                       -- If no meter's have delivered measurements later than this one, the period is still
                       --  active (null)
                       then null
                     else
                       -- Otherwise, the period ends at the time of this meter's last received measurement,
                       --   or the next one's earliest, whichever is the greatest. This is necessary to avoid
                       --   overlapping periods when faulty meters have delivered measurements with erroneous
                       --   (too early) timestamps
                       greatest(m.cmax, lead(m.cmin) over w)
                     end
                   )
               end as period
      from logical_meter
             join physical_meter
                  on (logical_meter.organisation_id = physical_meter.organisation_id
                    and logical_meter.id = physical_meter.logical_meter_id)
             join lateral (select max(created) as cmax, min(created) as cmin
                           from measurement
                           where physical_meter.id = measurement.physical_meter_id) m
                  on true window w as (partition by logical_meter.id order by m.cmax desc)
     ) p
where physical_meter.id = p.physical_meter_id;
