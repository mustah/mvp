import * as React from 'react';
import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {LookupState} from '../../../state/search/selection/selectionModels';
import {getNumAddresses, getNumCities} from '../../../state/search/selection/selectionSelectors';
import {RowCenter} from '../components/layouts/row/Row';
import {Summary} from '../components/summary/Summary';
import '../components/summary/Summary.scss';

interface StateToProps {
  numCities: number;
  numAddresses: number;
  numMeters: number;
}

const SummaryComponent = (props: StateToProps) => {
  const {numCities, numAddresses, numMeters} = props;
  return (
    <RowCenter className="SummaryComponent">
      <Summary title={translate('city', {count: numCities})} count={numCities}/>
      <Summary title={translate('address', {count: numAddresses})} count={numAddresses}/>
      <Summary title={translate('meter', {count: numMeters})} count={numMeters}/>
    </RowCenter>
  );
};

const mapStateToProps = ({domainModels: {meters, geoData}, searchParameters: {selection}}: RootState): StateToProps => {
  const lookupState: LookupState = {geoData, selection};
  return {
    numCities: getNumCities(lookupState),
    numAddresses: getNumAddresses(lookupState),
    numMeters: meters.total,
  };
};

export const SummaryContainer = connect<StateToProps, {}, {}>(mapStateToProps)(SummaryComponent);
