import {Dialog as MaterialDialog} from 'material-ui';
import * as React from 'react';
import {OnSelectCustomDateRange} from '../../state/user-selection/userSelectionModels';
import {OnClick, PickValue} from '../../types/Types';
import {ButtonCancel, ButtonConfirm} from '../buttons/DialogButtons';
import {CustomPeriodSelector, CustomPeriodSelectorProps} from '../dates/CustumPeriodSelector';

interface Props {
  isOpen: boolean;
  confirm: OnSelectCustomDateRange;
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

    // TODO: Should perhaps be moved to a helper file so that it can be tested.
    const confirmDisabled = !startDate || !endDate;

    const actions = [
      <ButtonCancel onClick={close} key="cancel"/>,
      <ButtonConfirm onClick={this.confirmAndClose} key="confirm" disabled={confirmDisabled}/>,
    ];

    return (
      <MaterialDialog
        actions={actions}
        autoScrollBodyContent={true}
        onRequestClose={close}
        open={isOpen}
      >
        <CustomPeriodSelector {...this.state} onFocusChange={this.onFocusChange} onDatesChange={this.onDatesChange}/>
      </MaterialDialog>
    );
  }

  confirmAndClose = () => {
    const {close, confirm} = this.props;
    const {startDate, endDate} = this.state;
    // Action button confirm disabled if endDate or startDate is null or undefined
    confirm({start: startDate!.toDate(), end: endDate!.toDate()});
    close();
  }
  onDatesChange = ({startDate, endDate}: Pick<State, 'startDate' | 'endDate'>) => this.setState({startDate, endDate});
  onFocusChange = (focusedInput: PickValue<State, 'focusedInput'>) => focusedInput && this.setState({focusedInput});
}
