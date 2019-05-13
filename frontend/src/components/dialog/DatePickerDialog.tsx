import {important} from 'csx';
import * as React from 'react';
import {classes, style} from 'typestyle';
import {colors, CssStyles} from '../../app/colors';
import {CallbackWith, OnClick, PickValue} from '../../types/Types';
import {ButtonCancel, ButtonConfirm} from '../buttons/DialogButtons';
import {CustomPeriodSelector, CustomPeriodSelectorProps} from '../dates/CustumPeriodSelector';
import {DateRange} from '../dates/dateModels';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {Dialog} from './Dialog';

const getContentClassName = ({primary}: CssStyles): string => {
  const calendarDaySelected = style({
    $nest: {
      '.CalendarDay__selected, .CalendarDay__selected:hover, .CalendarDay__selected:active': {
        background: important(primary.bgDark),
      },
    }
  });

  const calendarDaySelectedText = style({
    $nest: {
      '.CalendarDay__selected_span, .CalendarDay__selected_span:hover, .CalendarDay__selected_span:active': {
        background: important(primary.bgActive),
      },
    }
  });

  const calendarDayHoveredText = style({
    $nest: {
      '.CalendarDay__hovered_span': {background: important(primary.bgActive)},
      '.CalendarDay__hovered_span:active': {background: important(primary.bgDark)},
      '.CalendarDay__hovered_span:hover': {background: important(primary.bgActive), color: important(colors.white)},
    }
  });

  const dayPickerNavigationHorizontalButton = style({
    $nest: {
      '.DayPickerNavigation_button__horizontal:hover': {
        border: '1px solid',
        borderColor: primary.bg,
      },
      '.DayPickerNavigation_button__horizontal:hover .DayPickerNavigation_svg__horizontal': {
        fill: primary.bg,
      },
    },
  });

  return classes(
    'PeriodConfirmDialog',
    calendarDaySelected,
    calendarDaySelectedText,
    calendarDayHoveredText,
    dayPickerNavigationHorizontalButton,
  );
};

export interface Props extends ThemeContext {
  isOpen: boolean;
  confirm: CallbackWith<DateRange>;
  close: OnClick;
  startDate: PickValue<State, 'startDate'>;
  endDate: PickValue<State, 'endDate'>;
}

type State = CustomPeriodSelectorProps;

class PeriodConfirmDialog extends React.Component<Props, State> {

  state: State = {
    startDate: this.props.startDate,
    endDate: this.props.endDate,
    focusedInput: 'startDate',
  };

  componentWillReceiveProps({startDate, endDate}: Props) {
    this.setState({startDate, endDate});
  }

  render() {
    const {cssStyles, isOpen, close} = this.props;
    const {startDate, endDate} = this.state;

    const isDisabled = !(startDate && endDate);

    const actions = [
      <ButtonCancel onClick={close} key="cancel"/>,
      <ButtonConfirm onClick={this.confirmAndClose} key="confirm" disabled={isDisabled}/>,
    ];

    return (
      <Dialog
        contentClassName={getContentClassName(cssStyles)}
        isOpen={isOpen}
        autoScrollBodyContent={true}
        close={close}
        actions={actions}
      >
        <CustomPeriodSelector
          {...this.state}
          onFocusChange={this.onFocusChange}
          onDatesChange={this.onDatesChange}
        />
      </Dialog>
    );
  }

  confirmAndClose = () => {
    const {close, confirm} = this.props;
    const {startDate, endDate} = this.state;
    confirm({start: startDate!.toDate(), end: endDate!.toDate()});
    close();
  }

  onDatesChange = ({startDate, endDate}: Pick<State, 'startDate' | 'endDate'>) =>
    this.setState({startDate, endDate})

  onFocusChange = (focusedInput: PickValue<State, 'focusedInput'>) =>
    focusedInput && this.setState({focusedInput})
}

export const DatePickerDialog = withCssStyles((props: Props) => <PeriodConfirmDialog {...props}/>);
