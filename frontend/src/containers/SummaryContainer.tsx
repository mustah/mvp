import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Row} from '../components/layouts/row/Row';
import {SmallLoader} from '../components/loading/SmallLoader';
import {Summary} from '../components/summary/Summary';
import '../components/summary/Summary.scss';
import {now} from '../helpers/dateHelpers';
import {RootState} from '../reducers/rootReducer';
import {translate} from '../services/translationService';
import {fetchSummary} from '../state/summary/summaryApiActions';
import {SelectionSummary} from '../state/summary/summaryModels';
import {getSelectionSummary} from '../state/summary/summarySelection';
import {getMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch} from '../types/Types';

interface StateToProps {
  selectionSummary: SelectionSummary;
  parameters: EncodedUriParameters;
  isFetching: boolean;
}

interface DispatchToProps {
  fetchSummary: Fetch;
}

type Props = StateToProps & DispatchToProps;

class SummaryComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchSummary, parameters} = this.props;
    fetchSummary(parameters);
  }

  componentWillReceiveProps({fetchSummary, parameters}: Props) {
    fetchSummary(parameters);
  }

  render() {
    const {selectionSummary: {numMeters, numCities, numAddresses}, isFetching} = this.props;
    return (
      <Row className="SummaryContainer">
        <SmallLoader isFetching={isFetching}>
          <Summary title={translate('city', {count: numCities})} count={numCities}/>
          <Summary title={translate('address', {count: numAddresses})} count={numAddresses}/>
          <Summary title={translate('meter', {count: numMeters})} count={numMeters}/>
        </SmallLoader>
      </Row>
    );
  }
}

const mapStateToProps = ({userSelection: {userSelection}, summary}: RootState): StateToProps =>
  ({
    selectionSummary: getSelectionSummary(summary),
    isFetching: summary.isFetching,
    parameters: getMeterParameters({userSelection, now: now()}),
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSummary,
}, dispatch);

export const SummaryContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SummaryComponent);
