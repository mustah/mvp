import * as React from 'react';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {MainTitle, Subtitle} from '../../components/texts/Titles';
import {translate} from '../../services/translationService';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {Info} from './Info';

interface Props {
  meter: Meter;
}

export const MeterDetailsInfo = ({meter}: Props) => {
  const renderAlarm = () => meter.alarm
    ? <Info label={translate('alarm')} value={meter.alarm}/>
    : null;

  const renderReadInterval = () => {
    if (meter.readIntervalMinutes === 0) {
      return translate('unknown');
    } else if (meter.readIntervalMinutes >= 60) {
      return (meter.readIntervalMinutes / 60) + translate('hour in short');
    }

    return meter.readIntervalMinutes + translate('minute in short');
  };

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
          <Info label={translate('resolution')} value={renderReadInterval()}/>
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('validation')}</Subtitle>
            </Row>
          </Column>
          <Info
            label={translate('status')}
            value={<Status name={meter.status.name}/>}
          />
          {renderAlarm()}
        </Row>
        <Row>
          <Column>
            <Row>
              <Subtitle>{translate('labels')}</Subtitle>
            </Row>
          </Column>
          <Info label={translate('sap id')} value={meter.sapId}/>
          <Info label={translate('facility id')} value={meter.facility}/>
        </Row>
      </Column>
    </Row>
  );
};
