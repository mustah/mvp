import * as React from 'react';
import {connect} from 'react-redux';
import {RootState} from '../reducers/rootReducer';
import {translate} from '../services/translationService';
import {SelectionSummary} from '../state/search/selection/selectionModels';
import {getSelectionSummary} from '../state/search/selection/selectionSelectors';
import {Row} from '../components/layouts/row/Row';
import {Summary} from '../components/summary/Summary';
import '../components/summary/Summary.scss';

interface StateToProps {
  selectionSummary: SelectionSummary;
}

const SummaryComponent = (props: StateToProps) => {
  const {selectionSummary: {cities, addresses, meters}} = props;
  return (
    <Row className="SummaryContainer">
      <Summary title={translate('city', {count: cities})} count={cities}/>
      <Summary title={translate('address', {count: addresses})} count={addresses}/>
      <Summary title={translate('meter', {count: meters})} count={meters}/>
    </Row>
  );
};

const mapStateToProps = ({domainModels: {meters}}: RootState): StateToProps => {
  return {
    selectionSummary: getSelectionSummary(meters),
  };
};

export const SummaryContainer = connect<StateToProps>(mapStateToProps)(SummaryComponent);
