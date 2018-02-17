import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Row} from '../components/layouts/row/Row';
import {Summary} from '../components/summary/Summary';
import '../components/summary/Summary.scss';
import {RootState} from '../reducers/rootReducer';
import {translate} from '../services/translationService';
import {RestGet} from '../state/domain-models/domainModels';
import {fetchAllMeters} from '../state/domain-models/domainModelsActions';
import {SelectionSummary} from '../state/search/selection/selectionModels';
import {getEncodedUriParametersForAllMeters, getSelectionSummary} from '../state/search/selection/selectionSelectors';

interface StateToProps {
  selectionSummary: SelectionSummary;
  encodedUriParametersForAllMeters: string;
}

interface DispatchToProps {
  fetchAllMeters: RestGet;
}

type Props = StateToProps & DispatchToProps;

class SummaryComponent extends React.Component<Props> {
  componentDidMount() {
    const {fetchAllMeters, encodedUriParametersForAllMeters} = this.props;
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  componentWillReceiveProps({fetchAllMeters, encodedUriParametersForAllMeters}: Props) {
    fetchAllMeters(encodedUriParametersForAllMeters);
  }

  render() {
    const {selectionSummary: {cities, addresses, meters}} = this.props;
    return (
      <Row className="SummaryContainer">
        <Summary title={translate('city', {count: cities})} count={cities}/>
        <Summary title={translate('address', {count: addresses})} count={addresses}/>
        <Summary title={translate('meter', {count: meters})} count={meters}/>
      </Row>
    );
  }
}

const mapStateToProps = ({domainModels: {allMeters}, searchParameters}: RootState): StateToProps => {
  return {
    selectionSummary: getSelectionSummary(allMeters),
    encodedUriParametersForAllMeters: getEncodedUriParametersForAllMeters(searchParameters),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchAllMeters,
}, dispatch);

export const SummaryContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SummaryComponent);
