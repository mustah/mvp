import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DateRange, Period} from '../components/dates/dateModels';
import {PeriodSelection} from '../components/dates/PeriodSelection';
import {Maybe} from '../helpers/Maybe';
import {RootState} from '../reducers/rootReducer';
import {selectPeriod, setCustomDateRange} from '../state/user-selection/userSelectionActions';
import {OnSelectCustomDateRange, OnSelectPeriod} from '../state/user-selection/userSelectionModels';
import {getSelectedPeriod} from '../state/user-selection/userSelectionSelectors';

interface StateToProps {
  period: Period;
  customDateRange: Maybe<DateRange>;
}

interface DispatchToProps {
  selectPeriod: OnSelectPeriod;
  setCustomDateRange: OnSelectCustomDateRange;
}

const style: React.CSSProperties = {
  marginTop: 0,
  marginBottom: 0,
};

const PeriodComponent =
  ({selectPeriod, period, setCustomDateRange, customDateRange}: StateToProps & DispatchToProps) => (
    <PeriodSelection
      selectPeriod={selectPeriod}
      period={period}
      setCustomDateRange={setCustomDateRange}
      customDateRange={customDateRange}
      style={style}
    />);

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectPeriod,
  setCustomDateRange,
}, dispatch);

const mapStateToProps = ({userSelection: {userSelection}}: RootState): StateToProps => ({
  ...getSelectedPeriod(userSelection),
});

export const PeriodContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(PeriodComponent);
