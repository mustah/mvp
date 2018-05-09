import * as React from 'react';
import {imagePathFor} from '../../app/routes';
import {Column} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {MainTitle} from '../../components/texts/Titles';
import {orUnknown} from '../../helpers/translations';
import {translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {Info} from './Info';

interface Props {
  gateway: Gateway;
}

export const GatewayDetailsInfo =
  ({gateway: {location: {city, address}, serial, productModel, status}}: Props) => {
    const gatewayImage = imagePathFor('cme2110.jpg');

    return (
      <Column className="GatewayDetailsInfo">
        <Column className="Overview">
          <Row>
            <MainTitle>{translate('gateway details')}</MainTitle>
            <Info label={translate('gateway serial')} value={serial}/>
            <Info label={translate('product model')} value={productModel}/>
            <Info label={translate('city')} value={orUnknown(city.name)}/>
            <Info label={translate('address')} value={orUnknown(address.name)}/>
          </Row>
        </Column>
        <Row>
          <Column>
            <img src={gatewayImage} width={120}/>
          </Column>
          <Info
            label={translate('collection')}
            value={<Status name={status.name}/>}
          />
        </Row>
      </Column>
    );
  };
