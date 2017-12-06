import * as React from 'react';
import {translate} from '../../services/translationService';
import {IdNamed, statusFor} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';

export const Status = ({id, name}: IdNamed) => {
  return statusFor(id) && (
    <Row>
      <IconStatus id={id} name={translate(name)}/>
    </Row>
  );
};
