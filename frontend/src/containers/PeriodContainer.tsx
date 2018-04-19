import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../reducers/rootReducer';
import {selectPeriod} from '../state/search/selection/selectionActions';
import {OnSelectPeriod} from '../state/search/selection/selectionModels';
import {getSelectedPeriod} from '../state/search/selection/selectionSelectors';
import {Period} from '../components/dates/dateModels';
import {PeriodSelection} from '../components/dates/PeriodSelection';
import '../components/summary/Summary.scss';

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

const mapStateToProps = ({userSelection: {userSelection}}: RootState): StateToProps => {
  return {
    period: getSelectedPeriod(userSelection),
  };
};

export const PeriodContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(PeriodComponent);
