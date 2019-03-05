import * as React from 'react';
import {CallbackWith, OnClick, PickValue} from '../../types/Types';
import {ButtonCancel, ButtonConfirm} from '../buttons/DialogButtons';
import {CustomPeriodSelector, CustomPeriodSelectorProps} from '../dates/CustumPeriodSelector';
import {DateRange} from '../dates/dateModels';
import {Dialog} from './Dialog';

interface Props {
  isOpen: boolean;
  confirm: CallbackWith<DateRange>;
  close: OnClick;
  startDate: PickValue<State, 'startDate'>;
  endDate: PickValue<State, 'endDate'>;
}

type State = CustomPeriodSelectorProps;

export class PeriodConfirmDialog extends React.Component<Props, State> {

  state: State = {
    startDate: this.props.startDate,
    endDate: this.props.endDate,
    focusedInput: 'startDate',
  };

  componentWillReceiveProps({startDate, endDate}: Props) {
    this.setState({startDate, endDate});
  }

  render() {
    const {isOpen, close} = this.props;
    const {startDate, endDate} = this.state;

    const isDisabled = !(startDate && endDate);

    const actions = [
      <ButtonCancel onClick={close} key="cancel"/>,
      <ButtonConfirm onClick={this.confirmAndClose} key="confirm" disabled={isDisabled}/>,
    ];

    return (
      <Dialog
        contentClassName="PeriodConfirmDialog"
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
