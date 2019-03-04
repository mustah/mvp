import * as React from 'react';
import {Props} from '../../containers/SummaryContainer';
import {translate} from '../../services/translationService';
import {Row} from '../layouts/row/Row';
import {SmallLoader} from '../loading/SmallLoader';
import {Summary} from './Summary';

export const SummaryComponent = ({
  fetchSummary,
  parameters,
  isFetching,
  selectionSummary: {numMeters, numCities, numAddresses}
}: Props) => {
  React.useEffect(() => {
    fetchSummary(parameters);
  }, [parameters, numMeters, numCities, numAddresses]);

  return (
    <Row className="SummaryComponent">
      <SmallLoader isFetching={isFetching}>
        <Summary title={translate('city', {count: numCities})} count={numCities}/>
        <Summary title={translate('address', {count: numAddresses})} count={numAddresses}/>
        <Summary title={translate('meter', {count: numMeters})} count={numMeters}/>
      </SmallLoader>
    </Row>
  );
};
