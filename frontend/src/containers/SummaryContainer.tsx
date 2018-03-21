import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Row} from '../components/layouts/row/Row';
import {Summary} from '../components/summary/Summary';
import '../components/summary/Summary.scss';
import {RootState} from '../reducers/rootReducer';
import {translate} from '../services/translationService';
import {getEncodedUriParametersForAllMeters} from '../state/search/selection/selectionSelectors';
import {fetchSummary} from '../state/summary/summaryApiActions';
import {SelectionSummary} from '../state/summary/summaryModels';
import {getSelectionSummary} from '../state/summary/summarySelection';
import {Fetch} from '../types/Types';

interface StateToProps {
  selectionSummary: SelectionSummary;
  parameters: string;
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

const mapStateToProps = ({searchParameters, summary}: RootState): StateToProps => ({
  selectionSummary: getSelectionSummary(summary),
  parameters: getEncodedUriParametersForAllMeters(searchParameters),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSummary,
}, dispatch);

export const SummaryContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SummaryComponent);
