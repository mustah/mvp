import {Moment} from 'moment-timezone';
import * as React from 'react';
import {dropdownListStyle} from '../../app/themes';
import {momentAtUtcPlusOneFrom, prettyRange} from '../../helpers/dateHelpers';
import {Maybe} from '../../helpers/Maybe';
import {firstUpperTranslated, translate} from '../../services/translationService';
import {CallbackWith, Styled} from '../../types/Types';
import {DatePickerDialog} from '../dialog/DatePickerDialog';
import {DropdownMenu, MenuItemProps} from '../dropdown-selector/DropdownMenu';
import {DateRange, Period} from './dateModels';

interface NullableDateRange {
  start: Moment | null;
  end: Moment | null;
}

interface Props extends Styled {
  disabled?: boolean;
  customDateRange: Maybe<DateRange>;
  period: Period;
  selectPeriod: CallbackWith<Period>;
  setCustomDateRange: CallbackWith<DateRange>;
}

interface State {
  periodSelectorOpen: boolean;
}

export class PeriodSelection extends React.Component<Props, State> {

  state: State = {periodSelectorOpen: false};

  render() {
    const {customDateRange, disabled, period, style} = this.props;

    const timePeriods: MenuItemProps[] = [
      {
        value: Period.yesterday,
        label: firstUpperTranslated('yesterday'),
        primaryText: translate('yesterday'),
        onClick: () => this.onSelectPeriod(Period.yesterday),
      },
      {
        value: Period.previous7Days,
        label: prettyRange({period: Period.previous7Days, customDateRange}),
        primaryText: translate('last 7 days'),
        onClick: () => this.onSelectPeriod(Period.previous7Days),
      },
      {
        value: Period.currentMonth,
        label: prettyRange({period: Period.currentMonth, customDateRange}),
        primaryText: translate('this month'),
        onClick: () => this.onSelectPeriod(Period.currentMonth),
      },
      {
        value: Period.previousMonth,
        label: prettyRange({period: Period.previousMonth, customDateRange}),
        primaryText: translate('previous month'),
        onClick: () => this.onSelectPeriod(Period.previousMonth),
      },
      {
        hasDivider: true,
        value: Period.custom,
        label: prettyRange({period: Period.custom, customDateRange}),
        primaryText: translate('custom period'),
        onClick: this.openPeriodSelector,
      },
    ];

    const {start, end} = customDateRange.map<NullableDateRange>(({start, end}) => ({
      start: momentAtUtcPlusOneFrom(start),
      end: momentAtUtcPlusOneFrom(end),
    })).orElse({start: null, end: null});

    return (
      <DropdownMenu
        listStyle={dropdownListStyle}
        menuItems={timePeriods}
        value={period}
        style={style}
        disabled={disabled}
      >
        <DatePickerDialog
          isOpen={this.state.periodSelectorOpen}
          confirm={this.confirmCustomPeriod}
          close={this.closePeriodSelector}
          startDate={start}
          endDate={end}
        />
      </DropdownMenu>
    );
  }

  openPeriodSelector = () => this.setState({periodSelectorOpen: true});

  closePeriodSelector = () => this.setState({periodSelectorOpen: false});

  confirmCustomPeriod = (dateRange: DateRange) => {
    this.setState({periodSelectorOpen: false});
    this.props.setCustomDateRange(dateRange);
  }

  onSelectPeriod = (period: Period) => this.props.selectPeriod(period);
}
