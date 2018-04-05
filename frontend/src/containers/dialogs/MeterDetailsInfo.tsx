import * as React from 'react';
import {IconStatus} from '../../components/icons/IconStatus';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {MainTitle, Subtitle} from '../../components/texts/Titles';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {titleOf} from './dialogHelper';
import {Info} from './Info';

interface Props {
  meter: Meter;
}

export const MeterDetailsInfo = (props: Props) => {
  const {meter} = props;

  const renderAlarm = () => meter.alarm
    ? <Info label={translate('alarm')} value={meter.alarm}/>
    : null;

  const gateway = meter.gateway;
  const meterFlags = meter.flags || [];
  const meterStatus = meter.status;

  return (
    <Row>
      <Column className="OverView">
        <Row>
          <Column>
            <Row>
              <div className="display-none">{meter.id}</div>
              <MainTitle>{translate('meter')}</MainTitle>
            </Row>
          </Column>
          <Info label={translate('product model')} value={meter.manufacturer}/>
          <Info label={translate('medium')} value={meter.medium}/>
          <Info label={translate('city')} value={meter.location.city.name}/>
          <Info label={translate('address')} value={meter.location.address.name}/>
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('collection')}</Subtitle>
            </Row>
          </Column>
          <Info
            label={translate('status')}
            value={<IconStatus id={gateway.status.id} name={gateway.status.name}/>}
          />
          <Info label={translate('interval')} value="24h"/>
          <Info label={translate('resolution')} value="1h"/>
          {/*<Info label={translate('flagged for action')} value={titleOf(gateway.flags)}/>*/}
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('validation')}</Subtitle>
            </Row>
          </Column>
          <Info
            label={translate('status')}
            value={<IconStatus id={meterStatus.id} name={meterStatus.name}/>}
          />
          {renderAlarm()}
          <Info label={translate('flagged for action')} value={titleOf(meterFlags)}/>
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('labels')}</Subtitle>
            </Row>
          </Column>
          <Info label={translate('sap id')} value={meter.sapId}/>
          <Info label={translate('facility id')} value={meter.facility}/>
          <Info label={translate('measure id')} value={meter.measurementId}/>
        </Row>
      </Column>
    </Row>
  );
};
