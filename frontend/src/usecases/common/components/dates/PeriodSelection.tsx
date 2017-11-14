import {DropDownMenu, MenuItem} from 'material-ui';
import 'PeriodSelection.scss';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {OnSelectPeriod} from '../../../../state/search/selection/selectionModels';
import {Period} from '../../../../types/Types';
import {colors, fontSizeNormal, listItemStyle} from '../../../app/themes';
import {IconCalendar} from '../icons/IconCalendar';
import {Row} from '../layouts/row/Row';

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
      value: Period.now,
      chosen: 'Nu', // TODO demo purposes only. I imagine we will input real timestamps here in the future, anyways
      alternative: translate('now'),
    },
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
      style={listItemStyle}
    />
  ));

  return (
    <Row className="PeriodSelection">
      <DropDownMenu
        maxHeight={300}
        underlineStyle={underlineStyle}
        labelStyle={labelStyle}
        iconStyle={iconStyle}
        style={style}
        value={period}
        onChange={onSelectPeriod}
        iconButton={<IconCalendar className="IconCalendar"/>}
        selectedMenuItemStyle={{color: colors.blue}}
      >
        {timePeriodComponents}
      </DropDownMenu>
    </Row>
  );
};

const height = 32;

const style: React.CSSProperties = {
  height,
  width: 158,
  fontSize: fontSizeNormal,
  border: `1px solid ${colors.borderColor}`,
  borderRadius: 3,
  marginLeft: 24,
  marginBottom: 8,
};

const underlineStyle: React.CSSProperties = {
  border: 'none',
};

const labelStyle: React.CSSProperties = {
  height,
  lineHeight: 1,
  paddingRight: 0,
  paddingLeft: 16,
  fontSize: 14,
  display: 'flex',
  alignItems: 'center',
};

const iconStyle: React.CSSProperties = {
  fill: '#7b7b7b',
  height,
  width: 46,
  right: 0,
  top: 0,
  padding: 0,
};
