import * as React from 'react';
import {meterStatusTranslation} from '../../helpers/translations';
import {IdNamed, statusFor} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';

export const Status = (status: IdNamed) => {
  return (statusFor(status.id) && (
    <Row>
      <IconStatus id={status.id} name={meterStatusTranslation(status)}/>
    </Row>
  )) || null;
};
