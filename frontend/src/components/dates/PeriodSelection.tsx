import {DropDownMenu, MenuItem} from 'material-ui';
import * as React from 'react';
import {colors, fontSizeNormal, listItemStyle} from '../../app/themes';
import {prettyRange} from '../../helpers/dateHelpers';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {OnSelectPeriod} from '../../state/search/selection/selectionModels';
import {IconCalendar} from '../icons/IconCalendar';
import {Row} from '../layouts/row/Row';
import {Period} from './dateModels';
import './PeriodSelection.scss';

const height = 32;

const style: React.CSSProperties = {
  height,
  width: 210,
  fontSize: fontSizeNormal,
  border: `1px solid ${colors.borderColor}`,
  borderRadius: 3,
  marginLeft: 24,
  marginBottom: 16,
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
  fill: colors.lightBlack,
  height,
  width: 46,
  right: 0,
  top: 0,
  padding: 0,
};

const selectedMenuItemStyle: React.CSSProperties = {color: colors.blue};

interface Props {
  period: Period;
  selectPeriod: OnSelectPeriod;
}

interface TimePeriods {
  value: Period;
  chosen: string;
  alternative: string;
}

export class PeriodSelection extends React.Component<Props> {

  render() {
    const {period, selectPeriod} = this.props;

    const onSelectPeriod = (event, index: number, period: Period) => selectPeriod(period);

    const timePeriods: TimePeriods[] = [
      {
        value: Period.latest,
        chosen: firstUpperTranslated('last 24h'),
        alternative: translate('last 24h'),
      },
      {
        value: Period.currentMonth,
        chosen: prettyRange(Period.currentMonth),
        alternative: translate('current month'),
      },
      {
        value: Period.previousMonth,
        chosen: prettyRange(Period.previousMonth),
        alternative: translate('previous month'),
      },
      {
        value: Period.currentWeek,
        chosen: prettyRange(Period.currentWeek),
        alternative: translate('current week'),
      },
      {
        value: Period.previous7Days,
        chosen: prettyRange(Period.previous7Days),
        alternative: translate('last 7 days'),
      },
    ];

    const timePeriodComponents = timePeriods.map(({alternative, chosen, value}: TimePeriods) => (
      <MenuItem
        className="TimePeriod"
        key={alternative}
        label={chosen}
        primaryText={alternative}
        style={listItemStyle}
        value={value}
      />
    ));

    return (
      <Row className="PeriodSelection">
        <DropDownMenu
          className="PeriodSelection-dropdown"
          maxHeight={300}
          underlineStyle={underlineStyle}
          labelStyle={labelStyle}
          iconStyle={iconStyle}
          style={style}
          value={period}
          onChange={onSelectPeriod}
          iconButton={<IconCalendar className="IconCalendar"/>}
          selectedMenuItemStyle={selectedMenuItemStyle}
        >
          {timePeriodComponents}
        </DropDownMenu>
      </Row>
    );
  }
}
