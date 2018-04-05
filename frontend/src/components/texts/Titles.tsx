import * as React from 'react';
import {Row} from '../layouts/row/Row';
import {Bold, Large, Small, Xlarge} from './Texts';
import './Titles.scss';

interface TitleProps {
  children: React.ReactNode[] | React.ReactNode;
  className?: string;
  subtitle?: string;
}

export const MainTitle = ({children, subtitle}: TitleProps) => (
  <Row className="MainTitle">
    <Xlarge className="Bold">{children}</Xlarge>
    {subtitle && <Small className="Subtitle-description">{subtitle}</Small>}
  </Row>
);

export const Subtitle = ({children}: TitleProps) => (
  <Row className="Subtitle">
    <Bold>{children}</Bold>
  </Row>
);

export const MissingDataTitle = ({title}: {title: string}) =>
  <Large className="MissingDataTitle">{title}</Large>;
