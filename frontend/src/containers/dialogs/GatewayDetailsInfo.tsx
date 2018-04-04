import * as React from 'react';
import {assetsPathFor} from '../../app/routes';
import {Column, ColumnCenter} from '../../components/layouts/column/Column';
import {Row} from '../../components/layouts/row/Row';
import {Status} from '../../components/status/Status';
import {MainTitle} from '../../components/texts/Titles';
import {translate} from '../../services/translationService';
import {Gateway} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {titleOf} from './dialogHelper';
import {Info} from './Info';

interface Props {
  gateway: Gateway;
}

export const GatewayDetailsInfo = ({gateway}: Props) => {
  const {location: {city, address}, serial, productModel, status, flags} = gateway;
  const gatewayImage = assetsPathFor('cme2110.jpg');

  return (
    <div className="GatewayDetailsInfo">
      <Row className="space-between">
        <Column>
          <MainTitle>{translate('gateway details')}</MainTitle>
        </Column>
        <ColumnCenter>
          <Row className="Address">
            <Info label={translate('city')} value={city.name}/>
            <Info label={translate('address')} value={address.name}/>
          </Row>
        </ColumnCenter>
      </Row>
      <Row>
        <Column>
          <img src={gatewayImage} width={100}/>
        </Column>
        <Column className="OverView">
          <Row>
            <Info label={translate('gateway serial')} value={serial}/>
            <Info label={translate('product model')} value={productModel}/>
          </Row>
          <Row>
            <Info
              label={translate('collection')}
              value={<Status id={status.id} name={status.name}/>}
            />
            <Info label={translate('interval')} value={'24h'}/>
            <Info label={translate('flagged for action')} value={titleOf(flags)}/>
          </Row>
        </Column>
      </Row>
    </div>
  );
};
