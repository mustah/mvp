import * as React from 'react';
import {Children} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import {Medium, Small, Xlarge} from './Texts';
import './Titles.scss';

interface TitleProps {
  children: Children;
  className?: string;
  subtitle?: string;
}

export const MainTitle = ({children, subtitle}: TitleProps) => (
  <Row className="MainTitle">
    <Xlarge>{children}</Xlarge>
    {subtitle && <Small className="Subtitle-description">{subtitle}</Small>}
  </Row>
);

export const Subtitle = ({children}: TitleProps) => (
  <Row className="Subtitle">
    <Medium className="Bold first-uppercase">{children}</Medium>
  </Row>
);
