import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../app/routes';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {ButtonInfo} from '../buttons/ButtonInfo';
import {Row} from '../layouts/row/Row';

interface Props {
  meter: Meter;
  subPath?: string;
}

const labelStyle: React.CSSProperties = {
  textOverflow: 'ellipsis',
  maxWidth: 170,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
};

const iconStyle: React.CSSProperties = {
  height: 40,
  padding: 0,
};

export const MeterListItem = ({meter: {facility, id}, subPath = ''}: Props) =>
  (
    <Row>
      <Link to={`${routes.meter}/${id}${subPath}`} className="link">
        <ButtonInfo
          label={facility}
          iconStyle={iconStyle}
          labelStyle={labelStyle}
          title={facility.toString()}
        />
      </Link>
    </Row>
  );
