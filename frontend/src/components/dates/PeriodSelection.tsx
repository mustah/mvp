import {DropDownMenu, MenuItem} from 'material-ui';
import {Moment} from 'moment-timezone';
import * as React from 'react';
import {colors, fontSize, listItemStyle} from '../../app/themes';
import {momentWithTimeZone, now, prettyRange} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {OnSelectCustomDateRange, OnSelectPeriod} from '../../state/user-selection/userSelectionModels';
import {OnClick} from '../../types/Types';
import {PeriodConfirmDialog} from '../dialog/PeriodConfirmDialog';
import {IconCalendar} from '../icons/IconCalendar';
import {Row} from '../layouts/row/Row';
import {DateRange, Period} from './dateModels';
import './PeriodSelection.scss';

const height = 32;

const style: React.CSSProperties = {
  height,
  width: 210,
  fontSize: fontSize.normal,
  border: `2px solid ${colors.borderColor}`,
  borderRadius: 4,
  marginLeft: 24,
  marginBottom: 16,
  borderWidth: 1,
};

const listStyle: React.CSSProperties = {
  width: 200,
};

const underlineStyle: React.CSSProperties = {
  border: 'none',
};

const labelStyle: React.CSSProperties = {
  height,
  lineHeight: 1,
  paddingRight: 0,
  paddingLeft: 8,
  fontSize: 14,
  display: 'flex',
  alignItems: 'center',
  width: 210,
};

const iconStyle: React.CSSProperties = {
  fill: colors.lightBlack,
  height,
  width: 36,
  right: 0,
  top: 0,
  padding: 0,
};

const selectedMenuItemStyle: React.CSSProperties = {color: colors.blue};

interface Props {
  period: Period;
  selectPeriod: OnSelectPeriod;
  customDateRange: Maybe<DateRange>;
  setCustomDateRange: OnSelectCustomDateRange;
}

interface State {
  periodSelectorOpen: boolean;
}

interface TimePeriod {
  value: Period;
  chosen: string;
  alternative: string;
  onClick: OnClick;
}

interface NullableDateRange {
  start: Moment | null;
  end: Moment | null;
}

export class PeriodSelection extends React.Component<Props, State> {

  state: State = {periodSelectorOpen: false};

  render() {
    const {period, customDateRange} = this.props;

    const timePeriods: TimePeriod[] = [
      {
        value: Period.latest,
        chosen: firstUpperTranslated('last 24h'),
        alternative: translate('last 24h'),
        onClick: (event) => this.onSelectPeriod(event, Period.latest),
      },
      {
        value: Period.currentWeek,
        chosen: prettyRange({now: now(), period: Period.currentWeek, customDateRange}),
        alternative: translate('current week'),
        onClick: (event) => this.onSelectPeriod(event, Period.currentWeek),
      },
      {
        value: Period.previous7Days,
        chosen: prettyRange({now: now(), period: Period.previous7Days, customDateRange}),
        alternative: translate('last 7 days'),
        onClick: (event) => this.onSelectPeriod(event, Period.previous7Days),
      },
      {
        value: Period.currentMonth,
        chosen: prettyRange({now: now(), period: Period.currentMonth, customDateRange}),
        alternative: translate('current month'),
        onClick: (event) => this.onSelectPeriod(event, Period.currentMonth),
      },
      {
        value: Period.previousMonth,
        chosen: prettyRange({now: now(), period: Period.previousMonth, customDateRange}),
        alternative: translate('previous month'),
        onClick: (event) => this.onSelectPeriod(event, Period.previousMonth),
      },
      {
        value: Period.custom,
        chosen: prettyRange({now: now(), period: Period.custom, customDateRange}),
        alternative: translate('custom period'),
        onClick: this.openPeriodSelector,
      },
    ];

    const timePeriodComponents = timePeriods.map(({alternative, chosen, value, onClick}: TimePeriod) => (
      <MenuItem
        className="TimePeriod"
        key={alternative}
        label={chosen}
        primaryText={alternative}
        style={listItemStyle}
        value={value}
        onClick={onClick}
      />
    ));

    const {start, end} = customDateRange.map<NullableDateRange>(({start, end}) => ({
      start: momentWithTimeZone(start),
      end: momentWithTimeZone(end),
    })).orElse({start: null, end: null});

    return (
      <Row className="PeriodSelection">
        <DropDownMenu
          className="PeriodSelection-dropdown"
          maxHeight={300}
          underlineStyle={underlineStyle}
          labelStyle={labelStyle}
          listStyle={listStyle}
          iconStyle={iconStyle}
          style={style}
          value={period}
          iconButton={<IconCalendar className="IconCalendar"/>}
          selectedMenuItemStyle={selectedMenuItemStyle}
        >
          {timePeriodComponents}
        </DropDownMenu>
        <PeriodConfirmDialog
          isOpen={this.state.periodSelectorOpen}
          confirm={this.confirmCustomPeriod}
          close={this.closePeriodSelector}
          startDate={start}
          endDate={end}
        />
      </Row>
    );
  }

  openPeriodSelector = () => this.setState({periodSelectorOpen: true});

  closePeriodSelector = () => this.setState({periodSelectorOpen: false});

  confirmCustomPeriod = (dateRange: DateRange) => {
    this.setState({periodSelectorOpen: false});
    this.props.setCustomDateRange(dateRange);
  }

  onSelectPeriod = (event, period: Period) => this.props.selectPeriod(period);
}
