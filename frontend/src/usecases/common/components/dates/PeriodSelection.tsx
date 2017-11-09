import {DropDownMenu, MenuItem} from 'material-ui';
import 'PeriodSelection.scss';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {OnSelectPeriod} from '../../../../state/search/selection/selectionModels';
import {Period} from '../../../../types/Types';
import {colors} from '../../../app/themes';
import {IconCalendar} from '../icons/IconCalendar';
import {RowCenter} from '../layouts/row/Row';

interface Props {
  period: Period;
  selectPeriod: OnSelectPeriod;
}

export const PeriodSelection = (props: Props) => {
  const {period, selectPeriod} = props;

  const onSelectPeriod = (event, index, period: Period) => {
    selectPeriod(period);
  };

  const timePeriods = [
    {
      value: Period.currentMonth,
      chosen: '1 nov - 22 nov',
      alternative: translate('current month'),
    },
    {
      value: Period.previousMonth,
      chosen: '1 okt - 31 okt',
      alternative: translate('previous month'),
    },
    {
      value: Period.currentWeek,
      chosen: '20 nov - 22 nov',
      alternative: translate('current week'),
    },
    {
      value: Period.previous7Days,
      chosen: '16 nov - 22 nov',
      alternative: translate('last 7 days'),
    },
    {
      value: Period.custom,
      chosen: '1 okt - 31 okt',
      alternative: translate('pick a date'),
    },
  ];

  const timePeriodComponents = timePeriods.map((tp) => (
    <MenuItem
      key={tp.alternative}
      value={tp.value}
      label={tp.chosen}
      primaryText={tp.alternative}
      className="TimePeriod"
    />
  ));

  return (
    <RowCenter>
      <DropDownMenu
        maxHeight={300}
        underlineStyle={{border: 'none'}}
        labelStyle={{height: 48, lineHeight: '48px', paddingRight: 0, paddingLeft: 24, fontSize: 14}}
        iconStyle={{fill: 'black', height: 48, width: 48, right: 0, top: 0, padding: 0}}
        style={{width: 165}}
        className="PeriodSelection"
        value={period}
        onChange={onSelectPeriod}
        iconButton={<IconCalendar/>}
        selectedMenuItemStyle={{color: colors.blue}}
      >
        {timePeriodComponents}
      </DropDownMenu>
    </RowCenter>
  );
};
