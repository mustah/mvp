import * as React from 'react';
import {Row} from '../../../components/layouts/row/Row';
import {FirstUpper} from '../../../components/texts/Texts';
import './EmptyContentText.scss';

interface Props {
  text: string;
}

export const EmptyContentText = ({text}: Props) => (
  <Row className="EmptyContentText">
    <FirstUpper className="Italic">{text}</FirstUpper>
  </Row>
);
