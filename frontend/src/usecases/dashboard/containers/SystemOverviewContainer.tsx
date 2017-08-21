import * as React from 'react';
import {Bold, Xlarge} from '../../common/components/texts/Texts';
import {PeriodSelectionContainer} from '../../common/containers/PeriodSelectionContainer';
import {Row} from '../../layouts/components/row/Row';
import {StatusBox} from '../components/status/StatusBox';

export const SystemOverviewContainer = props => (
  <div>
    <Row>
      <Xlarge><Bold>Systemöversikt</Bold></Xlarge>
    </Row>
    <Row className="Row-right">
        <PeriodSelectionContainer/>
    </Row>
    <Row>
      <StatusBox title="Insamling (%)" count="3567 punkter" value="95.8" color="orange"/>
      <StatusBox title="Mätvärdeskvalitet (%)" count="3481 punkter" value="93.5" color="red"/>
      <StatusBox title="Connectorer (%)" count="4 st" value="100" color="green"/>

      <StatusBox title="Tidsupplösning" count="" value="" color="grey"/>
      <StatusBox title="Datafördröjning" count="" value="" color="grey"/>
    </Row>
  </div>
);
