import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import '../components/summary/Summary.scss';
import {SummaryComponent} from '../components/summary/SummaryComponent';
import {RootState} from '../reducers/rootReducer';
import {fetchSummary} from '../state/summary/summaryApiActions';
import {SelectionSummary} from '../state/summary/summaryModels';
import {getMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch} from '../types/Types';

export interface StateToProps {
  selectionSummary: SelectionSummary;
  parameters: EncodedUriParameters;
  isFetching: boolean;
}

export interface DispatchToProps {
  fetchSummary: Fetch;
}

const mapStateToProps = ({
  userSelection: {userSelection},
  summary: {payload, isFetching},
  search: {validation: {query}}
}: RootState): StateToProps =>
  ({
    selectionSummary: payload,
    isFetching,
    parameters: getMeterParameters({userSelection, query}),
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSummary,
}, dispatch);

export const SummaryContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SummaryComponent);
