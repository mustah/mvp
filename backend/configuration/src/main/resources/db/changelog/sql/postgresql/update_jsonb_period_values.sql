-- widget dateRange
update widget as w
set settings = jsonb_set(w.settings, '{selectionInterval}', '{"period": "yesterday"}', false)
where w.settings -> 'selectionInterval' ->> 'period' = 'latest';

update widget as w
set settings = jsonb_set(w.settings, '{selectionInterval}', '{"period": "yesterday"}', false)
where w.settings -> 'selectionInterval' ->> 'period' = 'current_week';

update widget as w
set settings = jsonb_set(w.settings, '{selectionInterval}', '{"period": "currentMonth"}', false)
where w.settings -> 'selectionInterval' ->> 'period' = 'current_month';

update widget as w
set settings = jsonb_set(w.settings, '{selectionInterval}', '{"period": "previousMonth"}', false)
where w.settings -> 'selectionInterval' ->> 'period' = 'previous_month';

update widget as w
set settings = jsonb_set(w.settings, '{selectionInterval}', '{"period": "previous7Days"}', false)
where w.settings -> 'selectionInterval' ->> 'period' = 'previous_7_days';

-- user selection selection parameters

-- dateRange
update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{dateRange}',
                                     '{"period": "yesterday"}', false)
where u.selection_parameters -> 'dateRange' ->> 'period' = 'latest';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{dateRange}',
                                     '{"period": "yesterday"}', false)
where u.selection_parameters -> 'dateRange' ->> 'period' = 'current_week';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{dateRange}',
                                     '{"period": "currentMonth"}', false)
where u.selection_parameters -> 'dateRange' ->> 'period' = 'current_month';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{dateRange}',
                                     '{"period": "previousMonth"}', false)
where u.selection_parameters -> 'dateRange' ->> 'period' = 'previous_month';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{dateRange}',
                                     '{"period": "previous7Days"}', false)
where u.selection_parameters -> 'dateRange' ->> 'period' = 'previous_7_days';

-- threshold
update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{threshold, dateRange}',
                                     '{"period": "yesterday"}', false)
where u.selection_parameters -> 'threshold' -> 'dateRange' ->> 'period' = 'latest';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{threshold, dateRange}',
                                     '{"period": "yesterday"}', false)
where u.selection_parameters -> 'threshold' -> 'dateRange' ->> 'period' = 'current_week';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{threshold, dateRange}',
                                     '{"period": "currentMonth"}', false)
where u.selection_parameters -> 'threshold' -> 'dateRange' ->> 'period' = 'current_month';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{threshold, dateRange}',
                                     '{"period": "previousMonth"}', false)
where u.selection_parameters -> 'threshold' -> 'dateRange' ->> 'period' = 'previous_month';

update user_selection as u
set selection_parameters = jsonb_set(u.selection_parameters, '{threshold, dateRange}',
                                     '{"period": "previous7Days"}', false)
where u.selection_parameters -> 'threshold' -> 'dateRange' ->> 'period' = 'previous_7_days';
