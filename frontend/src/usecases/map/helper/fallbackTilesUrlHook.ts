import * as React from 'react';

const swedishTilesUrl = 'https://{s}.tile.openstreetmap.se/hydda/full/{z}/{x}/{y}.png';
const fallbackTilesUrl = 'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png';
let currentTilesUrl = swedishTilesUrl;

export const useFallbackTilesUrl = () => {
  const [tilesUrl, setTileUrl] = React.useState<string>(currentTilesUrl);
  const updateTilesUrl = () => {
    if (tilesUrl !== fallbackTilesUrl) {
      currentTilesUrl = fallbackTilesUrl;
      setTileUrl(fallbackTilesUrl);
    }
  };
  return {tilesUrl, updateTilesUrl};
};
