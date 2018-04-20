import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../components/dates/dateModels';
import {PeriodSelection} from '../components/dates/PeriodSelection';
import '../components/summary/Summary.scss';
import {RootState} from '../reducers/rootReducer';
import {selectPeriod} from '../state/user-selection/userSelectionActions';
import {OnSelectPeriod} from '../state/user-selection/userSelectionModels';

interface StateToProps {
  period: Period;
}

interface DispatchToProps {
  selectPeriod: OnSelectPeriod;
}

const PeriodComponent = (props: StateToProps & DispatchToProps) => {
  const {selectPeriod, period} = props;
  return (<PeriodSelection selectPeriod={selectPeriod} period={period}/>);
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectPeriod,
}, dispatch);

const mapStateToProps = ({userSelection: {userSelection: {selectionParameters: {period}}}}: RootState): StateToProps =>
  ({period});

export const PeriodContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(PeriodComponent);
