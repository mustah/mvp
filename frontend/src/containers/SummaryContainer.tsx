import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import '../components/summary/Summary.scss';
import {SummaryComponent} from '../components/summary/SummaryComponent';
import {RootState} from '../reducers/rootReducer';
import {fetchSummary} from '../state/summary/summaryApiActions';
import {SelectionSummary} from '../state/summary/summaryModels';
import {allCurrentMeterParameters, getMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch} from '../types/Types';

export interface StateToProps {
  selectionSummary: SelectionSummary;
  parameters: EncodedUriParameters;
  isFetching: boolean;
}

export interface DispatchToProps {
  fetchSummary: Fetch;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = (amount: 'all' | 'selection') => ({
  userSelection: {userSelection},
  summary: {payload, isFetching},
  search: {validation: {query}}
}: RootState): StateToProps =>
  ({
    selectionSummary: payload,
    isFetching,
    parameters: amount === 'selection'
      ? getMeterParameters({userSelection, query})
      : allCurrentMeterParameters
    ,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSummary,
}, dispatch);

export const SummaryContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps('selection'), mapDispatchToProps)(SummaryComponent);

export const IgnoreSelectionSummaryContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps('all'), mapDispatchToProps)(SummaryComponent);
