import * as React from 'react';
import {imagePathFor} from '../../app/routes';
import {Column, ColumnCenter} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {MainTitle} from '../../components/texts/Titles';
import {locationNameTranslation} from '../../helpers/translations';
import {translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Info} from './Info';

interface Props {
  gateway: Gateway;
}

export const GatewayDetailsInfo = ({gateway}: Props) => {
  const {location: {city, address}, serial, productModel, status} = gateway;
  const gatewayImage = imagePathFor('cme2110.jpg');

  return (
    <div className="GatewayDetailsInfo">
      <Row className="space-between">
        <Column>
          <MainTitle>{translate('gateway details')}</MainTitle>
        </Column>
        <ColumnCenter>
          <Row className="Address">
            <Info label={translate('city')} value={locationNameTranslation(city.name)}/>
            <Info label={translate('address')} value={locationNameTranslation(address.name)}/>
          </Row>
        </ColumnCenter>
      </Row>
      <Row>
        <Column>
          <img src={gatewayImage} width={100}/>
        </Column>
        <Column className="Overview">
          <Row>
            <Info label={translate('gateway serial')} value={serial}/>
            <Info label={translate('product model')} value={productModel}/>
          </Row>
          <Row>
            <Info
              label={translate('collection')}
              value={<Status name={status.name}/>}
            />
          </Row>
        </Column>
      </Row>
    </div>
  );
};
