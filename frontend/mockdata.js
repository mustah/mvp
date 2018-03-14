module.exports = () => {
  return {
  dashboards: [
    {
      id: 3,
      widgets: [
        {
          type: 'collection',
          total: 1697,
          status: 'warning',
          pending: 22,
        },
        {
          type: 'measurementQuality',
          total: 1709,
          status: 'critical',
          pending: 6,
        },
      ],
    },
  ],
};
};
