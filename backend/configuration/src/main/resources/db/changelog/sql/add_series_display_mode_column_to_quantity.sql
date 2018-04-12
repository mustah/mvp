ALTER TABLE quantity
  ADD series_display_mode INTEGER default 0;

UPDATE quantity
SET series_display_mode = (case
                           when name in ('Energy', 'Volume')
                             then 2
                           else 1
                           end);
