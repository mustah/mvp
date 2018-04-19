import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Row} from '../components/layouts/row/Row';
import {Summary} from '../components/summary/Summary';
import '../components/summary/Summary.scss';
import {RootState} from '../reducers/rootReducer';
import {translate} from '../services/translationService';
import {getMeterParameters} from '../state/user-selection/userSelectionSelectors';
import {fetchSummary} from '../state/summary/summaryApiActions';
import {SelectionSummary} from '../state/summary/summaryModels';
import {getSelectionSummary} from '../state/summary/summarySelection';
import {EncodedUriParameters, Fetch} from '../types/Types';

interface StateToProps {
  selectionSummary: SelectionSummary;
  parameters: EncodedUriParameters;
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
    const {selectionSummary: {numMeters, numCities, numAddresses}} = this.props;
    return (
      <Row className="SummaryContainer">
        <Summary title={translate('city', {count: numCities})} count={numCities}/>
        <Summary title={translate('address', {count: numAddresses})} count={numAddresses}/>
        <Summary title={translate('meter', {count: numMeters})} count={numMeters}/>
      </Row>
    );
  }
}

const mapStateToProps = ({userSelection, summary}: RootState): StateToProps => ({
  selectionSummary: getSelectionSummary(summary),
  parameters: getMeterParameters(userSelection),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSummary,
}, dispatch);

export const SummaryContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SummaryComponent);
